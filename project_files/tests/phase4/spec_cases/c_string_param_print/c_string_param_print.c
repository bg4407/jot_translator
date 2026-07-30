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

void bar(char* s) {
printf("%s\n", s);
}
int main(void) {
bar("hello");
    return 1;
}
