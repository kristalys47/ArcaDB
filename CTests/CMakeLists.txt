cmake_minimum_required(VERSION 3.16)
project(CTests)
set(CMAKE_CXX_STANDARD 14)

include_directories (CTests)
add_subdirectory (ORC)


add_executable(CTests main.cpp ORCManagement.cpp ORCManagement.h)

target_link_libraries (CTests ORC)


#file(GLOB SOURCES /orc/c++/include/*.hxx /orc/c++/include/*.cxx /orc/c++/src/*.hxx /orc/c++/src/*.cxx).

#include_directories(
#        /orc/c++/src
#        /orc/c++/include
#        /build/c++/src
#        /build/c++/include
#)





#target_link_libraries (orc
#        orc::lz4
#        orc::protobuf
#        orc::snappy
#        orc::zlib
#        orc::gmock
#        )