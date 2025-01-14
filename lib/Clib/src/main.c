#include "main.h"
#include <stdlib.h>
#include <windows.h>
#include <minwindef.h>
#include <windowsx.h>
#include <wingdi.h>
#include <winnt.h>
#include <winuser.h>

char* readFile(char* path) {
    wchar_t* wpath = UTF8ToUTF16(path);
    HANDLE hFile = CreateFileW(
        wpath, 
        GENERIC_READ, 
        FILE_SHARE_READ, 
        NULL, 
        OPEN_EXISTING, 
        FILE_ATTRIBUTE_NORMAL, 
        NULL
    );
    free(wpath);
    if (hFile == INVALID_HANDLE_VALUE) {
        return NULL;
    }
    
    DWORD fileSize = GetFileSize(hFile, NULL);
    if (fileSize == INVALID_FILE_SIZE) {
        CloseHandle(hFile);
        return NULL;
    }

    // 使用 malloc 分配足够大的内存来存储整个文件的内容
    char* buffer = (char*)malloc(fileSize + 1); // +1 为了确保以 null 结尾
    if (buffer == NULL) {
        CloseHandle(hFile);
        return NULL;
    }

    // 从文件中读取内容
    DWORD bytesRead;
    BOOL readResult = ReadFile(
        hFile,                // 文件句柄
        buffer,               // 存储内容的缓冲区
        fileSize,             // 读取字节数
        &bytesRead,           // 实际读取字节数
        NULL                  // 异步操作时使用重叠结构
    );

    if (!readResult || bytesRead != fileSize) {
        free(buffer);
        CloseHandle(hFile);
        return NULL;
    }

    // 确保缓冲区以 null 结尾
    buffer[bytesRead] = '\0';

    // 关闭文件句柄
    CloseHandle(hFile);
    return buffer;
}



char* GBKToUTF8(char* gbkStr) {
    #define CP_GBK 936
    // 先将GBK字符串转换为宽字符
    int wideCharLen = MultiByteToWideChar(CP_GBK, 0, gbkStr, -1, NULL, 0);
    if (wideCharLen == 0) {
        return NULL; // 错误处理
    }

    wchar_t* wideCharStr = (wchar_t*)malloc(wideCharLen * sizeof(wchar_t));
    if (wideCharStr == NULL) {
        return NULL; // 内存分配失败
    }
    MultiByteToWideChar(CP_GBK, 0, gbkStr, -1, wideCharStr, wideCharLen);

    // 然后将宽字符转换为UTF-8
    int utf8Len = WideCharToMultiByte(CP_UTF8, 0, wideCharStr, -1, NULL, 0, NULL, NULL);
    if (utf8Len == 0) {
        free(wideCharStr);
        return NULL; // 错误处理
    }

    char* utf8Str = (char*)malloc(utf8Len);
    if (utf8Str == NULL) {
        free(wideCharStr);
        return NULL; // 内存分配失败
    }
    WideCharToMultiByte(CP_UTF8, 0, wideCharStr, -1, utf8Str, utf8Len, NULL, NULL);

    free(wideCharStr);
    return utf8Str;
}

wchar_t* UTF8ToUTF16(char* utf8Str) {
    // 计算所需的宽字符长度
    int wideCharLen = MultiByteToWideChar(CP_UTF8, 0, utf8Str, -1, NULL, 0);
    if (wideCharLen == 0) {
        return NULL; // 错误处理
    }

    // 分配足够的空间来存储宽字符字符串
    wchar_t* wideCharStr = (wchar_t*)malloc(wideCharLen * sizeof(wchar_t));
    if (wideCharStr == NULL) {
        return NULL; // 内存分配失败
    }

    // 执行转换
    MultiByteToWideChar(CP_UTF8, 0, utf8Str, -1, wideCharStr, wideCharLen);

    return wideCharStr;
}


void freeStr(char* str) {
    free(str);
}





void getTouchInfo(unsigned long long wParam, TouchInfo *ptr){
    POINTER_TOUCH_INFO pointerInfo;
    UINT32 id = GET_POINTERID_WPARAM(wParam);
    if(GetPointerTouchInfo(id, &pointerInfo)){
        ptr->id = id;
        ptr->pointX = pointerInfo.pointerInfo.ptPixelLocation.x;
        ptr->pointY = pointerInfo.pointerInfo.ptPixelLocation.y;
    }
}
int getMouseX(long long lParam){
    return GET_X_LPARAM(lParam);
}
int getMouseY(long long lParam){
    return GET_Y_LPARAM(lParam);
}

void touchInput(LPARAM lParam,WPARAM wParam,TouchInfos *p){
    HTOUCHINPUT hTouchInput = (HTOUCHINPUT)lParam;
    TOUCHINPUT *pTouches = NULL;
    pTouches = (TOUCHINPUT*)malloc(sizeof(TOUCHINPUT) * LOWORD(wParam));
    if (GetTouchInputInfo(hTouchInput, LOWORD(wParam), pTouches,sizeof(TOUCHINPUT))) {
        p->infos = malloc(sizeof(TouchInfo) * LOWORD(wParam));
        p->flags = malloc(sizeof(unsigned long) * LOWORD(wParam));
        p->size = LOWORD(wParam);
        for (UINT i = 0; i < LOWORD(wParam); i++) {
            p->infos[i].id = pTouches[i].dwID;
            p->infos[i].pointX = pTouches[i].x / 100;
            p->infos[i].pointY = pTouches[i].y / 100;
            p->flags[i] = pTouches[i].dwFlags;
        }
    }
    free(pTouches);
    CloseTouchInputHandle(hTouchInput);
}
void destructTouchInfos(TouchInfos *infos){
    free(infos->infos);
    free(infos->flags);
}