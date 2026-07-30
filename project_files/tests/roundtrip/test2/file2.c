#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>

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
    return 0;
}
