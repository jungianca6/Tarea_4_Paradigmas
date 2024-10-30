cmake_minimum_required(VERSION 3.29)
project(Tarea4 C)

set(CMAKE_C_STANDARD 11)

set(RAYLIB_DIR "/usr/local")
set(CJSON_DIR "/usr/local/include")

include_directories(${RAYLIB_DIR}/include)
include_directories(${CJSON_DIR})

link_directories(${RAYLIB_DIR}/lib)

add_executable(Tarea4 main.c
        Client/data.h
        Client/client.h
        Client/client.c
        INI/ini.c
        Config/config.h
        Config/config.c
        Client/register_data.h
        Client/parties_data.h
        Client/partie.h
        Client/end_data.h)

target_link_libraries(Tarea4 raylib m pthread dl cjson)