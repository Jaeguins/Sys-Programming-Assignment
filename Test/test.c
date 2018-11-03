#include <stdio.h>

int i = 0, j = 1;
int main() {
    f(&i, &j);
    printf("%d,%d\n", i, j);
    getchar();
    getchar();
}