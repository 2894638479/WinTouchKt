#include <d2d1.h>
#include <d2d1helper.h>
#include <dcommon.h>
#include <dwrite.h>

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
    cvt(*target)->SetDpi(96,96);
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
    const auto code = cvt(factory)->CreateTextFormat(
        (wchar_t*)fontFamily,
        NULL,
        (DWRITE_FONT_WEIGHT)weight,
        (DWRITE_FONT_STYLE)style,
        (DWRITE_FONT_STRETCH)strech,
        fontSize,
        L"zh-CN",
        cvt(format)
    );
    if(code == 0){
        cvt(*format)->SetTextAlignment(DWRITE_TEXT_ALIGNMENT_CENTER);
        cvt(*format)->SetParagraphAlignment(DWRITE_PARAGRAPH_ALIGNMENT_CENTER);
    }
    return code;
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

void d2dDrawRect(d2dDrawRectPara* para,float outlineWidth){
    cvt(para->target)->DrawRectangle(D2D1::RectF(para->l,para->t,para->r,para->b),cvt(para->brush),outlineWidth);
}
void d2dFillRect(d2dDrawRectPara* para){
    cvt(para->target)->FillRectangle(D2D1::RectF(para->l,para->t,para->r,para->b),cvt(para->brush));
}

void d2dFillRoundedRect(d2dDrawRectPara* para,float rx,float ry){
    cvt(para->target)->SetAntialiasMode(D2D1_ANTIALIAS_MODE_ALIASED);
    cvt(para->target)->FillRoundedRectangle(D2D1::RoundedRect(D2D1::RectF(para->l,para->t,para->r,para->b), rx, ry),cvt(para->brush));
    cvt(para->target)->SetAntialiasMode(D2D1_ANTIALIAS_MODE_PER_PRIMITIVE);
}
void d2dDrawRoundedRect(d2dDrawRectPara* para,float rx,float ry,float outlineWidth){
    cvt(para->target)->DrawRoundedRectangle(D2D1::RoundedRect(D2D1::RectF(para->l,para->t,para->r,para->b), rx, ry),cvt(para->brush),outlineWidth);
}

void d2dDrawRound(d2dDrawRoundPara*para,float outlineWidth){
    cvt(para->target)->DrawEllipse(D2D1::Ellipse(D2D1::Point2F(para->x,para->y), para->rx, para->ry),cvt(para->brush),outlineWidth);
}
void d2dFillRound(d2dDrawRoundPara*para){
    cvt(para->target)->FillEllipse(D2D1::Ellipse(D2D1::Point2F(para->x,para->y), para->rx, para->ry),cvt(para->brush));
}
void d2dDrawText(d2dDrawRectPara* para,d2dTextFormatHolder* format,unsigned short* text){
    cvt(para->target)->DrawText(
        (wchar_t*)text,
        wcslen((wchar_t*)text),
        cvt(format),
        D2D1::RectF(para->l,para->t,para->r,para->b),
        cvt(para->brush)
    );
}

D2D1_ANTIALIAS_MODE antialiasMode(bool enable){
    if(enable) return D2D1_ANTIALIAS_MODE_PER_PRIMITIVE;
    return D2D1_ANTIALIAS_MODE_ALIASED;
}

void d2dPushClip(d2dDrawRectPara* para,bool antialias){
    auto clipRect = D2D1::Rect(para->l, para->t, para->r, para->b);
    cvt(para->target)->PushAxisAlignedClip(clipRect, antialiasMode(antialias));
}

void d2dPopClip(d2dTargetHolder* target){
    cvt(target)->PopAxisAlignedClip();
}

void d2dSetAntialiasMode(d2dTargetHolder* target,bool enable){
    cvt(target)->SetAntialiasMode(antialiasMode(enable));
}

Point d2dGetDpi(d2dTargetHolder* target){
    Point dpi;
    cvt(target)->GetDpi(&dpi.x, &dpi.y);
    return dpi;
}

void d2dClear(d2dTargetHolder* target){
    cvt(target)->Clear();
}

}