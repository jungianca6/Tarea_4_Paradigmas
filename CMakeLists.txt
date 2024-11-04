cmake_minimum_required(VERSION 3.29)
project(Tarea_4_Paradigmas)

set(CMAKE_CXX_STANDARD 14)

include_directories(Tarea4/Client)
include_directories(Tarea4/Components)
include_directories(Tarea4/Config)
include_directories(Tarea4/INI)
include_directories(Tarea4/INI/cpp)

add_executable(Tarea_4_Paradigmas
    Tarea4/Client/bricks_data.h
    Tarea4/Client/client.c
    Tarea4/Client/client.h
    Tarea4/Client/data.h
    Tarea4/Client/end_data.h
    Tarea4/Client/partie.h
    Tarea4/Client/parties_data.h
    Tarea4/Client/player_data.h
    Tarea4/Client/register_data.h
    Tarea4/Components/ball.h
    Tarea4/Components/brick.h
    Tarea4/Components/brick_array.h
    Tarea4/Components/brick_factory.h
    Tarea4/Components/player.h
    Tarea4/Components/power_type.h
    Tarea4/Components/timer.h
    Tarea4/Config/config.c
    Tarea4/Config/config.h
    Tarea4/INI/cpp/INIReader.cpp
    Tarea4/INI/cpp/INIReader.h
    Tarea4/INI/examples/ini_dump.c
    Tarea4/INI/examples/ini_example.c
    Tarea4/INI/examples/ini_xmacros.c
    Tarea4/INI/examples/INIReaderExample.cpp
    Tarea4/INI/fuzzing/inihfuzz.c
    Tarea4/INI/tests/unittest.c
    Tarea4/INI/tests/unittest_alloc.c
    Tarea4/INI/tests/unittest_string.c
    Tarea4/INI/ini.c
    Tarea4/INI/ini.h
    Tarea4/main.c)
