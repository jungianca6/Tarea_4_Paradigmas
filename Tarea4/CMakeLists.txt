cmake_minimum_required(VERSION 3.29)
project(Tarea4 C)

set(CMAKE_C_STANDARD 11)

set(RAYLIB_DIR "~/Downloads/raylib-5.0_linux_amd64")

include_directories(${RAYLIB_DIR}/include)

link_directories(${RAYLIB_DIR}/lib)

add_executable(Tarea4 main.c)

target_link_libraries(Tarea4 raylib m pthread dl)
