LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE:= dataDecoder
LOCAL_SRC_FILES:= datadecode.cpp dataProgress.cpp
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_EXPORT_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
