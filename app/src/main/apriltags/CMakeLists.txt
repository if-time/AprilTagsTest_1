# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ../cpp/apriltag_jni.c
        apriltag.c
        apriltag_pose.c
        apriltag_quad_thresh.c
        tag16h5.c
        tag25h9.c
        tag36h11.c
        tagCircle21h7.c
        tagCircle49h12.c
        tagCustom48h12.c
        tagStandard41h12.c
        tagStandard52h13.c
        common/g2d.c
        common/getopt.c
        common/homography.c
        common/image_u8.c
        common/image_u8x3.c
        common/image_u8x4.c
        common/matd.c
        common/pam.c
        common/pjpeg.c
        common/pjpeg-idct.c
        common/pnm.c
        common/string_util.c
        common/svd22.c
        common/time_util.c
        common/unionfind.c
        common/workerpool.c
        common/zarray.c
        common/zhash.c
        common/zmaxheap.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})