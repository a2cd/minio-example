// 分片大小 5MB
const partSize = 5 * 1024 * 1024;

/**
 * 上传文件
 */
const uploadFile = () => {
    // 获取用户选择的文件
    const file = document.getElementById("upload").files[0];
    let ctx = {}
    // 获取文件md5
    calcFileMD5(file).then(md5 => {
        console.log("md5:", md5)
        ctx.md5 = md5
        // 根据md5查询文件是否已经上传
        return queryFileExists(md5)
    }).then(res => {
        const data = res.data
        if (data.exists) {
            console.log("文件已上传, 秒传完成, downloadUrl: " + data.downloadUrl)
            return Promise.reject({complete: true})
        }

        // 文件小于5MB直接走普通文件上传的逻辑
        if (file.size < partSize) {
            console.log("文件小于5MB, 走普通上传逻辑")
            return createPresignedUpload(file).then(res => {
                return createPresignedUploadComplete(file, ctx.md5)
            }).then(res => {
                const data = res.data
                console.log("文件上传完成, downloadUrl: " + data.downloadUrl)
            })
        }

        // 如果文件没有上传则后端向minio注册分片上传
        return createPartsUpload(file).then(res => {
            const data = res
            // 文件分片异步上传完成, 请求后端进行分片合并
            console.log("所有分片上传完成, 请求后端合并分片...")
            return mergeParts(data.uploadId, data.file.name, data.partCount, data.file.size, data.file.contentType, ctx.md5)
        }).then(res => {
            const data = res.data
            console.log("文件上传完成, downloadUrl: " + data.downloadUrl)
        })
    }).catch(e => {
        if (e.complete) {
            // 正常退出
        } else {
            console.error(e)
        }
    })
}

/**
 * 查询文件是否存在
 */
const queryFileExists = (md5) => {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: `http://localhost:8080/minio/v1/file-exists/${md5}`,
            type: 'GET',
            success: res => {
                resolve(res)
            }
        })
    })
}

/**
 * 创建预上传
 */
const createPresignedUpload = (file) => {
    return new Promise((resolve, reject) => {
        const params = JSON.stringify({objectName: file.name})
        $.ajax({
            url: "http://localhost:8080/minio/v1/presigned-upload",
            type: 'GET',
            data: params,
            success: res => {
                const data = res.data
                $.ajax({
                    url: data.presignedUploadUrl, type: 'PUT', contentType: false, processData: false, data: file,
                    success: () => {
                        resolve()
                    }
                })
            }
        })
    })
}

/**
 * 预上传完成后调用上传信息
 */
const createPresignedUploadComplete = (file, md5) => {
    return new Promise((resolve, reject) => {
        const params = JSON.stringify({objectName: file.name, md5: md5})
        $.ajax({
            url: "http://localhost:8080/minio/v1/presigned-upload/complete",
            type: 'POST',
            data: params,
            success: res => {
                resolve(res)
            }
        })
    })
}


const createPartsUpload = (file) => {
    // 计算当前选择文件需要的分片数量
    const partCount = Math.ceil(file.size / partSize)
    console.log(`fileSize: ${file.size / 1024 / 1024}MB, 分片数: ${partCount}`)
    if (partCount > 1000) {
        alert("上传的文件必须小于5GB")
        return Promise.reject("上传的文件必须小于5GB")
    }

    return new Promise((resolve, reject) => {
        const params = JSON.stringify({partCount: partCount, objectName: file.name})
        $.ajax({
            url: "http://localhost:8080/minio/v1/parts/create",
            type: 'POST',
            contentType: "application/json",
            processData: false,
            data: params,
            success: res => {
                const data = res.data
                console.log("uploadId:", data.uploadId)
                let list = []
                for (item of data.parts) {
                    let start = (item.partNumber - 1) * partSize
                    let end = Math.min(file.size, start + partSize)
                    // 取文件指定范围内的byte，从而得到分片数据
                    let _chunkFile = file.slice(start, end)
                    let p = new Promise((resolve, reject) => {
                        const num = Number(item.partNumber)
                        $.ajax({
                            url: item.uploadUrl, type: 'PUT', contentType: false, processData: false, data: _chunkFile,
                            success: () => {
                                console.log("第" + num + "个分片上传完成")
                                resolve()
                            }
                        })
                    })
                    list.push(p)
                }
                Promise.all(list).then(() => {
                    const data = {uploadId: res.data.uploadId, partCount, file}
                    resolve(data)
                })
            }
        })
    })
}

/**
 * 请求后端合并文件
 */
const mergeParts = (uploadId, fileName, partSize, fileSize, contentType, md5) => {
    return new Promise((resolve, reject) => {
        const params = JSON.stringify({
            uploadId: uploadId,
            objectName: fileName,
            partSize: partSize,
            objectSize: fileSize,
            contentType: contentType,
            expire: 12,
            maxGetCount: 2,
            md5: md5
        })
        $.ajax({
            url: "http://localhost:8080/minio/v1/parts/merge",
            type: 'POST',
            contentType: "application/json",
            processData: false,
            data: params,
            success: res => {
                resolve(res)
            }
        })
    })
}

/**
 * 计算文件MD5
 */
const calcFileMD5 = (file) => {
    let fileReader = new FileReader()
    fileReader.readAsBinaryString(file)
    let spark = new SparkMD5()
    return new Promise((resolve) => {
        fileReader.onload = (e) => {
            spark.appendBinary(e.target.result)
            resolve(spark.end())
        }
    })
}
