#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>

char* jott_concat(const char* a, const char* b) {
    size_t lenA = strlen(a);
    size_t lenB = strlen(b);
    char* out = (char*)malloc(lenA + lenB + 1);
    if (out == NULL) {
        return NULL;
    }
    memcpy(out, a, lenA);
    memcpy(out + lenA, b, lenB);
    out[lenA + lenB] = '\0';
    return out;
}

int foo(int x) {
return x + 1;
}
int main(void) {
    int x;
x = 0;
while (x < 10) {
x = foo(x);
printf("%d\n", x);
}
    return 1;
}
