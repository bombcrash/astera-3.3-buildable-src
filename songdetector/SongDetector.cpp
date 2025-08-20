#include "SongDetector.h"
#include <jni.h>
#include <string>
#include <thread>
#include <windows.h>

#include <winrt/Windows.Media.Control.h>
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Foundation.Collections.h>
#include <winrt/base.h>

using namespace winrt;
using namespace Windows::Media::Control;
using namespace Windows::Foundation;

// Helper function to convert wstring to UTF-8 string
std::string wstring_to_utf8(const std::wstring& wstr) {
    if (wstr.empty()) return std::string();
    
    int size_needed = WideCharToMultiByte(CP_UTF8, 0, wstr.data(), (int)wstr.size(), nullptr, 0, nullptr, nullptr);
    std::string strTo(size_needed, 0);
    WideCharToMultiByte(CP_UTF8, 0, wstr.data(), (int)wstr.size(), &strTo[0], size_needed, nullptr, nullptr);
    
    return strTo;
}

std::string getCurrentMediaInfo() {
    std::string result = "Playing media not found";
    try {
        std::thread t([&result]() {
            try {
                winrt::init_apartment(winrt::apartment_type::multi_threaded);
                auto manager = GlobalSystemMediaTransportControlsSessionManager::RequestAsync().get();
                auto sessions = manager.GetSessions();
                uint32_t count = sessions.Size();
                for (uint32_t i = 0; i < count; ++i) {
                    auto session = sessions.GetAt(i);
                    auto info = session.GetPlaybackInfo();
                    auto status = info.PlaybackStatus();
                    if (status == GlobalSystemMediaTransportControlsSessionPlaybackStatus::Playing) {
                        auto mediaProperties = session.TryGetMediaPropertiesAsync().get();
                        std::wstring title = mediaProperties.Title().c_str();
                        std::wstring artist = mediaProperties.Artist().c_str();
                        std::wstring res = title + L" - " + artist;
                        // Use proper UTF-16 to UTF-8 conversion
                        result = wstring_to_utf8(res);
                        return;
                    }
                }
            } catch (...) {

            }
        });
        t.join();
    } catch (...) {

    }
    return result;
}

JNIEXPORT jstring JNICALL Java_me_lyrica_utils_system_NativeMusicInfo_getCurrentSong(JNIEnv *env, jclass) {
    std::string song = getCurrentMediaInfo();
    return env->NewStringUTF(song.c_str());
} 