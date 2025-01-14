

char* readFile(char* path);
char* GBKToUTF8(char* gbkStr);
unsigned short* UTF8ToUTF16(char* utf8Str);
void freeStr(char* str);





typedef struct {
    unsigned int id;
    int pointX;
    int pointY;
} TouchInfo;
typedef struct {
    int size;
    TouchInfo *infos;
    unsigned long *flags;
} TouchInfos;
void destructTouchInfos(TouchInfos* infos);
void getTouchInfo(unsigned long long wParam,TouchInfo *ptr);
int getMouseX(long long lParam);
int getMouseY(long long lParam);
void touchInput(long long lParam,unsigned long long wParam,TouchInfos *p);
