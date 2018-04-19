# PhotoPicker
一款图片选择器，支持单选、多选、裁剪、适配7.0、适配小米

## Step 1. Add the JitPack repository to your build file

### Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```  
## Step 2. Add the dependency
```
	dependencies {
	        compile 'com.github.jarryleo:PhotoPicker:v2.0.2'
	}
```

# 注意：
本库依赖以下三个库，如果你的项目里面没有这3个依赖请加上，否则会崩溃，glide版本低于4.0
```
    implementation 'com.github.bumptech.glide:glide:3.7.0'
    implementation 'com.android.support:appcompat-v7:26.1.0'
    implementation 'com.android.support:design:26.1.0'
```
