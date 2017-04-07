# WalkCount  

项目中采用三种计步器的方式， TYPE_ACCELEROMETER，TYPE_GYROSCOPE，TYPE_STEP_DETECTOR，三种传感器计步。  
TYPE_ACCELEROMETER（线性加速度器），在手机中最常见，支持的也最早，现在大部分计步的程序都是基于它开发的，利用这个传感器，一种方式是计零值，两个零值计一步， 另一种是计波峰值，间隔时间。  
TYPE_STEP_DETECTOR 是Google 在4.4 之后加的专门用于计步的功能，实际测试中会有一些偏差。  
TYPE_GYROSCOPE（角速度传感器），也计零值，目前没实现这个功能。
