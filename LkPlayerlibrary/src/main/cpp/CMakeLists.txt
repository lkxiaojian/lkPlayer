
cmake_minimum_required(VERSION 3.10.2)

include_directories(include)
project("lkplayer")
file(GLOB SOURCE "*.cpp" "include/myinclude/*cpp")

add_library(
        lkplayer
        SHARED
        ${SOURCE})

set(my_lib_path ${CMAKE_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI})
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${my_lib_path}")

find_library(log-lib
        log)


target_link_libraries(
        lkplayer
        -Wl,--start-group
        avformat avcodec avfilter  avutil swresample swscale
        -Wl,--end-group
        ${log-lib}
        android
        z
        OpenSLES
)
