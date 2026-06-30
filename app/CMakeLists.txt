cmake_minimum_required(VERSION 3.22.1)
project(jvk_audio_engine VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Oboe — fetched directly from source (most reliable method) ──
# Official Google audio library: https://github.com/google/oboe
include(FetchContent)
FetchContent_Declare(
    oboe
    GIT_REPOSITORY https://github.com/google/oboe.git
    GIT_TAG        1.9.3
)
FetchContent_MakeAvailable(oboe)

# ── Source files ──
add_library(
    jvk_audio_engine
    SHARED

    src/main/cpp/AudioEngine.cpp
    src/main/cpp/SynthVoice.cpp
    src/main/cpp/Reverb.cpp
    src/main/cpp/Delay.cpp
    src/main/cpp/JvkAudioBridge.cpp
)

# ── Link libraries ──
target_link_libraries(
    jvk_audio_engine
    oboe
    android
    log
    atomic
)

target_include_directories(jvk_audio_engine PRIVATE
    src/main/cpp
    ${oboe_SOURCE_DIR}/include
)

# ── Compiler optimizations for audio performance ──
target_compile_options(jvk_audio_engine PRIVATE
    -O3
    -ffast-math
    -funroll-loops
    -fno-exceptions
    -DNDEBUG
)
