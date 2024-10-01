#include "main.h"
#include <stdlib.h>
#include <windows.h>
#include <minwindef.h>
#include <windowsx.h>
#include <winuser.h>

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