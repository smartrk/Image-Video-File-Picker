# Image-Video-File-Picker



# Android Image,Video,File picker with compress functionality
## 

Support Android 6 to 14 version

## Features
- Supports image,video and any document file
- Single and multiple selection support
- Image compress
- Auto handle permission
- List contines path,file and uri

- No Additional Code Required
- Fully Customizable
- Easy to Implement

## Code Sample
 - MainActivity
  
 ```sh
    Initialize picker when activity start/create.
    
     val picker = Picker(this)
```

 * Pick single Image
 ```sh
                    picker.pickImage(
                    originalFilesCallBack = { list ->
                        // return only one image in list
                    }, compressedFilesCallBack = { list ->
                       // return only one compressed image in list                        
                    })
```
* Pick multiple images
 ```sh
                    picker.pickImage(
                    multipleSelection = true,
                    originalFilesCallBack = { list ->
                     // return multiple images in list
                    }, compressedFilesCallBack = { list ->
                       // return multiple compressed image in list    
                    })
```
* Pick Video
 ```sh
                   picker.pickVideo(callBack = { list ->
                    // return only one image in list
                    })
```
 * Pick Multiple Videos
 ```sh
                   picker.pickMultipleVideo(callBack = { list ->
                     // return multiple videos in list
                    })
```
 * Capture Image From Camera
 ```sh
                    picker.captureImage(
                    originalFilesCallBack = { list ->
                       // return only one image in list
                    }, compressedFilesCallBack = { list ->
                       // return only one compressed image in list                        
                    })
```
  * Capture Video from camera
 ```sh
                   picker.captureVideo(
                    callBack = { list ->
                       // return only one video in list
                    }
                )
```
  * Pick file
 ```sh
                  picker.pickFile(mimeTypes = arrayOf("application/pdf", "text"),
                    callBack = { list ->
                        // return only one file
                    }
                )
```  
* Capture Video from camera
 ```sh
                  picker.pickMultipleFile(mimeTypes = arrayOf("application/pdf", "text"),
                    callBack = { list ->
                        // return multiple files
                    }
                )
```
* Pick image and video 
 ```sh
                  picker.pickImageAndVideo(
                    multipleSelection = true,
                    originalFilesCallBack = { list ->
                        clearAndSetUpText(list)
                    }, compressedFilesCallBack = { list ->
                        addText(list)
                    })
                    // For Single selection just removed multipleSelection or pass false.
```




## Gradle dependency

```sh
dependencies { 
         implementation 'com.github.smartrk:Image-Video-File-Picker:1.3' 
}
```
