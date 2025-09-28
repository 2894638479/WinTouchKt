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
    char* buffer = (char*)malloc(fileSize + 1);
    if (buffer == NULL) {
        CloseHandle(hFile);
        return NULL;
    }
    DWORD bytesRead;
    BOOL readResult = ReadFile(
        hFile,
        buffer,
        fileSize,
        &bytesRead,
        NULL
    );

    if (!readResult || bytesRead != fileSize) {
        free(buffer);
        CloseHandle(hFile);
        return NULL;
    }
    buffer[bytesRead] = '\0';
    CloseHandle(hFile);
    return buffer;
}

int writeFile(char* path,char* content) {
    if (path == NULL || content == NULL) {
        return -1;
    }

    wchar_t* wpath = UTF8ToUTF16(path);
    HANDLE hFile = CreateFileW(
        wpath,
        GENERIC_WRITE,
        0,
        NULL,
        CREATE_ALWAYS,
        FILE_ATTRIBUTE_NORMAL,
        NULL
    );
    free(wpath);
    
    if (hFile == INVALID_HANDLE_VALUE) {
        return -1;
    }
    
    DWORD bytesWritten;
    BOOL success = WriteFile(
        hFile,
        content,
        (DWORD)strlen(content),
        &bytesWritten,
        NULL
    );
    
    if (!success || bytesWritten != strlen(content)) {
        CloseHandle(hFile);
        return -1;
    }
    
    CloseHandle(hFile);
    return 0;
}



char* GBKToUTF8(char* gbkStr) {
    #define CP_GBK 936
    int wideCharLen = MultiByteToWideChar(CP_GBK, 0, gbkStr, -1, NULL, 0);
    if (wideCharLen == 0) {
        return NULL;
    }

    wchar_t* wideCharStr = (wchar_t*)malloc(wideCharLen * sizeof(wchar_t));
    if (wideCharStr == NULL) {
        return NULL;
    }
    MultiByteToWideChar(CP_GBK, 0, gbkStr, -1, wideCharStr, wideCharLen);
    int utf8Len = WideCharToMultiByte(CP_UTF8, 0, wideCharStr, -1, NULL, 0, NULL, NULL);
    if (utf8Len == 0) {
        free(wideCharStr);
        return NULL;
    }
    char* utf8Str = (char*)malloc(utf8Len);
    if (utf8Str == NULL) {
        free(wideCharStr);
        return NULL;
    }
    WideCharToMultiByte(CP_UTF8, 0, wideCharStr, -1, utf8Str, utf8Len, NULL, NULL);
    free(wideCharStr);
    return utf8Str;
}

wchar_t* UTF8ToUTF16(char* utf8Str) {
    int wideCharLen = MultiByteToWideChar(CP_UTF8, 0, utf8Str, -1, NULL, 0);
    if (wideCharLen == 0) {
        return NULL;
    }
    wchar_t* wideCharStr = (wchar_t*)malloc(wideCharLen * sizeof(wchar_t));
    if (wideCharStr == NULL) {
        return NULL;
    }
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