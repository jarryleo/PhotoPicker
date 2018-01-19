# PhotoPicker
一款图片选择器，支持单选、多选、裁剪、适配7.0、适配小米

依赖方法：全局build里加上这个仓库地址


allprojects {

	repositories {
        
		...
                
		maven { url 'https://jitpack.io' } //加上这句就行
                
	}
        
}
  
  依赖：
  {
##  	compile 'com.github.jarryleo:PhotoPicker:v1.0'
  }
   

调用方法：

#   PhotoPicker.selectPic(上下文，选择图片张数，是否裁剪，裁剪宽，裁剪高，图片地址回调)；
