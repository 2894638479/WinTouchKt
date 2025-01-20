typedef struct{} d2dFactoryHolder;
typedef struct{} d2dTargetHolder;
typedef struct{} d2dSolidColorBrushHolder;
typedef struct{} d2dWriteFactoryHolder;
typedef struct{} d2dTextFormatHolder;
typedef struct{} hwndHolder;

enum FONT_WEIGHT:unsigned short {
    FONT_WEIGHT_THIN = 100,
    FONT_WEIGHT_EXTRA_LIGHT = 200,
    // FONT_WEIGHT_ULTRA_LIGHT = 200,
    FONT_WEIGHT_LIGHT = 300,
    FONT_WEIGHT_SEMI_LIGHT = 350,
    FONT_WEIGHT_NORMAL = 400,
    // FONT_WEIGHT_REGULAR = 400,
    FONT_WEIGHT_MEDIUM = 500,
    FONT_WEIGHT_DEMI_BOLD = 600,
    // FONT_WEIGHT_SEMI_BOLD = 600,
    FONT_WEIGHT_BOLD = 700,
    FONT_WEIGHT_EXTRA_BOLD = 800,
    // FONT_WEIGHT_ULTRA_BOLD = 800,
    FONT_WEIGHT_BLACK = 900,
    // FONT_WEIGHT_HEAVY = 900,
    FONT_WEIGHT_EXTRA_BLACK = 950,
    // FONT_WEIGHT_ULTRA_BLACK = 950
};

enum FONT_STRETCH:unsigned char {
    FONT_STRETCH_UNDEFINED = 0,
    FONT_STRETCH_ULTRA_CONDENSED = 1,
    FONT_STRETCH_EXTRA_CONDENSED = 2,
    FONT_STRETCH_CONDENSED = 3,
    FONT_STRETCH_SEMI_CONDENSED = 4,
    // FONT_STRETCH_NORMAL = 5,
    FONT_STRETCH_MEDIUM = 5,
    FONT_STRETCH_SEMI_EXPANDED = 6,
    FONT_STRETCH_EXPANDED = 7,
    FONT_STRETCH_EXTRA_EXPANDED = 8,
    FONT_STRETCH_ULTRA_EXPANDED = 9
};

enum FONT_STYLE:unsigned char {
    FONT_STYLE_NORMAL,
    FONT_STYLE_OBLIQUE,
    FONT_STYLE_ITALIC
};



long d2dCreateFactory(d2dFactoryHolder** factory);
long d2dCreateTarget(d2dFactoryHolder* factory, d2dTargetHolder** target, hwndHolder* hwnd);
void d2dResizeRenderTarget(d2dTargetHolder* pRenderTarget,hwndHolder* hwnd);
long d2dCreateWriteFactory(d2dWriteFactoryHolder** factory);
long d2dCreateSolidColorBrush(d2dTargetHolder* pRenderTarget,d2dSolidColorBrushHolder** brush,float r,float g,float b,float a);
long d2dCreateTextFormat(
    d2dWriteFactoryHolder* factory,
    d2dTextFormatHolder** format,
    unsigned short* fontFamily,
    float fontSize,
    enum FONT_WEIGHT weight,
    enum FONT_STYLE style,
    enum FONT_STRETCH strech
);

void d2dFreeFactory(d2dFactoryHolder*);
void d2dFreeTarget(d2dTargetHolder*);
void d2dFreeSolidColorBrush(d2dSolidColorBrushHolder*);
void d2dFreeWriteFactory(d2dWriteFactoryHolder*);
void d2dFreeTextFormat(d2dTextFormatHolder*);

void d2dBeginDraw(d2dTargetHolder* target);
void d2dEndDraw(d2dTargetHolder* target);

typedef struct{
    d2dTargetHolder *target;
    d2dSolidColorBrushHolder* brush;
    float l;float t;float r;float b;
}d2dDrawRectPara;

void d2dDrawRect(d2dDrawRectPara* para,float outlineWidth);
void d2dFillRect(d2dDrawRectPara* para);
void d2dDrawText(d2dDrawRectPara* para,d2dTextFormatHolder* format,unsigned short* text);
void d2dFillRoundedRect(d2dDrawRectPara* para,float rx,float ry);
void d2dDrawRoundedRect(d2dDrawRectPara* para,float rx,float ry,float outlineWidth);

typedef struct{
    d2dTargetHolder *target;
    d2dSolidColorBrushHolder *brush;
    float x;float y;float rx; float ry;
}d2dDrawRoundPara;

void d2dDrawRound(d2dDrawRoundPara*para,float outlineWidth);
void d2dFillRound(d2dDrawRoundPara*para);

typedef union{
    d2dDrawRectPara rect;
    d2dDrawRoundPara round;
}d2dDrawParaBuffer;


typedef struct {
    float x;
    float y;
} Point;

#include <stdbool.h>

void d2dPushClip(d2dDrawRectPara* para,bool antialias);
void d2dPopClip(d2dTargetHolder* target);
void d2dSetAntialiasMode(d2dTargetHolder* target,bool enable);


Point d2dGetDpi(d2dTargetHolder* target);

void d2dClear(d2dTargetHolder* target);
