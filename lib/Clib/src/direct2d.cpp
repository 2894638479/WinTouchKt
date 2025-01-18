#include <cstddef>
#include <d2d1.h>
#include <d2d1helper.h>
#include <dwrite.h>
#include <unknwn.h>
#include <vcruntime_typeinfo.h>
#include <windef.h>
#include <winerror.h>
#include <wingdi.h>
#include <winnt.h>
#include <winuser.h>

extern "C" {
#include "direct2d.h"
}

template<typename T>
struct convert;

template<>struct convert<d2dFactoryHolder*>{using type = ID2D1Factory *;};
template<>struct convert<d2dTargetHolder*>{using type = ID2D1HwndRenderTarget *;};
template<>struct convert<hwndHolder*>{using type = HWND;};
template<>struct convert<HWND>{using type = hwndHolder*;};
template<>struct convert<d2dWriteFactoryHolder*>{using type = IDWriteFactory *;};
template<>struct convert<d2dTextFormatHolder*>{using type = IDWriteTextFormat *;};
template<>struct convert<d2dSolidColorBrushHolder*>{using type = ID2D1SolidColorBrush *;};

template <typename T>
typename convert<T>::type cvt(T t){
    return (typename  convert<T>::type)t;
}
template <typename T>
typename convert<T>::type* cvt(T* t){
    return (typename  convert<T>::type*)t;
}


extern "C" {

unsigned int _fltused = 1;

HRESULT d2dCreateFactory(d2dFactoryHolder** factory){
    return D2D1CreateFactory(D2D1_FACTORY_TYPE_SINGLE_THREADED, cvt(factory));
}
HRESULT d2dCreateTarget(d2dFactoryHolder* factory, d2dTargetHolder** target, hwndHolder* hwnd){
    long hr =  cvt(factory)->CreateHwndRenderTarget(
        D2D1::RenderTargetProperties(),
        D2D1::HwndRenderTargetProperties(cvt(hwnd), D2D1::SizeU(0, 0)),
        cvt(target)
    );
    if(FAILED(hr)) return hr;
    d2dResizeRenderTarget(*target, hwnd);
    return S_OK;
}


void d2dResizeRenderTarget(d2dTargetHolder* pRenderTarget,hwndHolder* hwnd){
    if (pRenderTarget == nullptr) return;
    RECT rect;
    GetClientRect(cvt(hwnd), &rect);
    cvt(pRenderTarget)->Resize(D2D1::SizeU(rect.right - rect.left, rect.bottom - rect.top));
}

HRESULT d2dCreateWriteFactory(d2dWriteFactoryHolder** factory){
    return DWriteCreateFactory(DWRITE_FACTORY_TYPE_SHARED, __uuidof(IDWriteFactory), (IUnknown**)factory);
}


HRESULT d2dCreateSolidColorBrush(d2dTargetHolder* pRenderTarget,d2dSolidColorBrushHolder** brush,float r,float g,float b,float a){
    return (cvt(pRenderTarget))->CreateSolidColorBrush(D2D1::ColorF(r,g,b,a),cvt(brush));
}


HRESULT d2dCreateTextFormat(
    d2dWriteFactoryHolder* factory,
    d2dTextFormatHolder** format,
    unsigned short* fontFamily,
    float fontSize,
    enum FONT_WEIGHT weight,
    enum FONT_STYLE style,
    enum FONT_STRETCH strech
){
    return cvt(factory)->CreateTextFormat(
        (wchar_t*)fontFamily,
        NULL,
        (DWRITE_FONT_WEIGHT)weight,
        (DWRITE_FONT_STYLE)style,
        (DWRITE_FONT_STRETCH)strech,
        fontSize,
        L"zh-CN",
        (IDWriteTextFormat **)format
    );
}

void d2dFreeFactory(d2dFactoryHolder* p){
    cvt(p)->Release();
}
void d2dFreeTarget(d2dTargetHolder* p){
    cvt(p)->Release();
}
void d2dFreeSolidColorBrush(d2dSolidColorBrushHolder* p){
    cvt(p)->Release();
}
void d2dFreeWriteFactory(d2dWriteFactoryHolder* p){
    cvt(p)->Release();
}
void d2dFreeTextFormat(d2dTextFormatHolder* p){
    cvt(p)->Release();
}


void d2dBeginDraw(d2dTargetHolder* target){
    cvt(target)->BeginDraw();
}
void d2dEndDraw(d2dTargetHolder* target){
    cvt(target)->EndDraw();
}


void d2dDrawRect(d2dTargetHolder* target,d2dSolidColorBrushHolder* brush,float l,float t,float r,float b){
    cvt(target)->FillRectangle(D2D1::RectF(l,t,r,b), cvt(brush));
}

void d2dDrawText(d2dTargetHolder* target,d2dSolidColorBrushHolder* brush,d2dTextFormatHolder* format,unsigned short* text,float l,float t,float r,float b){
    cvt(target)->DrawText(
        (wchar_t*)text,                    // 要绘制的文字
        wcslen((wchar_t*)text),            // 文字长度
        cvt(format),             // 文字格式
        D2D1::RectF(l,t,r,b),  // 绘制区域
        cvt(brush)                  // 文字颜色
    );
}

Point d2dGetDpi(d2dTargetHolder* target){
    Point dpi;
    cvt(target)->GetDpi(&dpi.x, &dpi.y);
    return dpi;
}

/*
LRESULT (*wndProcPtr)(hwndHolder *, unsigned int, unsigned long long, long long);
void d2dSetWndProc(LRESULT (*func)(hwndHolder *, unsigned int, unsigned long long, long long)){
    wndProcPtr = func;
}

LRESULT WindowProc(HWND__ *p0, unsigned int p1, unsigned long long p2, long long p3)
{
    return wndProcPtr(cvt(p0),p1,p2,p3);
}
hwndHolder* d2dWindowStep1() {
    HINSTANCE hInstance = GetModuleHandle(NULL);

    // 设置 WNDCLASSEX 结构体
    WNDCLASSEX wcex = {};
    wcex.cbSize = sizeof(WNDCLASSEX);
    wcex.lpfnWndProc = WindowProc;  // 窗口过程
    wcex.hInstance = hInstance;
    wcex.lpszClassName = "SampleWindowClass";  // 窗口类名
    wcex.hCursor = LoadCursor(NULL, IDC_ARROW);  // 默认光标
    wcex.hbrBackground = (HBRUSH)(COLOR_WINDOW+1);  // 背景色
    wcex.hIcon = LoadIcon(NULL, IDI_APPLICATION);  // 默认图标
    wcex.hIconSm = LoadIcon(NULL, IDI_APPLICATION);  // 小图标

    // 创建窗口
    HWND hwnd = CreateWindowEx(
        WS_EX_TOOLWINDOW | WS_EX_LAYERED | WS_EX_TOPMOST | WS_EX_NOACTIVATE,  // 扩展样式
        "SampleWindowClass",  // 使用已注册的窗口类
        "Sample Window",  // 窗口标题
        WS_OVERLAPPEDWINDOW,  // 窗口样式
        CW_USEDEFAULT, CW_USEDEFAULT, 500, 500,  // 窗口尺寸
        NULL, NULL, hInstance, NULL  // 父窗口、菜单、实例句柄、附加参数
    );
    RegisterTouchWindow(hwnd, TWF_WANTPALM | TWF_FINETOUCH);

    return cvt(hwnd);
}

void d2dWindowStep2(hwndHolder*hwnd){
    ShowWindow(cvt(hwnd), SW_SHOW);
    UpdateWindow(cvt(hwnd));
    MSG msg = { };
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
}
*/

void d2dClear(d2dTargetHolder* target){
    cvt(target)->Clear();
}


}