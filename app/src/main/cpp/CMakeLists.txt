
cmake_minimum_required(VERSION 3.10.2)
project("lkplayer")
file(GLOB SOURCE "*.cpp")
include_directories(include)

add_library(
        lkplayer
        SHARED
        ${SOURCE})



set(my_lib_path  ${CMAKE_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI})
set(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -L${my_lib_path}")
find_library(log-lib
        log)


target_link_libraries(
        lkplayer
        avfilter avformat avcodec avutil swresample swscale
        ${log-lib}
      )

#avfilter avformat avcodec avutil swresample swscale
