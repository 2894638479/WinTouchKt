typedef struct {
    unsigned int id;
    int pointX;
    int pointY;
} TouchInfo;
void getTouchInfo(unsigned long long wParam,TouchInfo *ptr);
int getMouseX(long long lParam);
int getMouseY(long long lParam);

