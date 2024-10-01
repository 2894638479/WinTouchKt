#include "main.h"
#include <windows.h>
#include <windowsx.h>

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