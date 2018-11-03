#include<stdio.h>
#include <stdlib.h>

typedef struct node {
    void* next;
    int data;
}Node;
void change(Node **list) {
    Node *preLast = *list, *first = *list;
    while (1) {
        if (((Node*)(preLast->next))->next == NULL) break;
        else preLast = preLast->next;
    }
    *list = preLast->next;//last
    ((Node*)preLast->next)->next = first;
    preLast->next = NULL;
}

void rearrange(Node *list)
{
    Node *p, *q;
    int  temp;
    if (list == NULL || list->next == NULL)return;
    p = list;
    q = list->next;
    while (q != NULL)
    {
        temp = p->data;
        p->data = q->data;
        q->data = temp;
        p = q->next;
        q = (p != NULL ? p->next : 0);
    }
}


Node *concatenate(Node *first, Node *second) {
    Node *pointer = first, *inputPointer = NULL, *ret = NULL;
    int i = 1;
    while (i-- >= 0) {
        while (1) {
            if (pointer == NULL) break;
            if (inputPointer == NULL) {
                inputPointer = (Node*)malloc(sizeof(Node));
                ret = inputPointer;
            }
            else {
                inputPointer->next = malloc(sizeof(Node));
                inputPointer = (Node*)inputPointer->next;
            }
            inputPointer->data = pointer->data;
            pointer = (Node*)pointer->next;
        }
        pointer = second;
    }
    inputPointer->next = NULL;
    return ret;
}
void RemoveRange(Node* list, int lower, int upper) {
    lower -= 1;
    Node *start = list;
    while (start->data < lower) start = start->next;
    if (start == NULL)return;
    Node *end = start->next;
    while (end != NULL && end->data <= upper) {
        Node *tmp = end;
        end = end->next;
        free(tmp);
    }
    start->next = end;
}
void func(Node *head)
{
    Node *p = head;
    while (p != NULL) {
        if (p->data < 0) {
            Node* tmp = p->next;
            p->data = tmp->data;
            p->next = tmp->next;
            free(tmp);
        }
        else p = p->next;
    }
}
Node *merge(Node *first, Node *second) {
    Node *ret;
    Node *returner;
    Node **tmp;
    tmp = (first->data < second->data) ? &first : &second;
    ret = *tmp;
    returner = ret;
    *tmp = (*tmp)->next;
    while (!(first == NULL && second == NULL)) {
        if (first == NULL)tmp = &second;
        else if (second == NULL)tmp = &first;
        else tmp = (first == NULL && !second == NULL && first->data < second->data) ? &first : &second;
        ret->next = *tmp;
        ret = ret->next;
        *tmp = (*tmp)->next;
    }
    return returner;
}
int main() {
    Node* listA, *listB, *pointerA, *pointerB;
    listA = (Node*)malloc(sizeof(Node));
    listB = (Node*)malloc(sizeof(Node));
    pointerA = listA;
    pointerB = listB;
    for (int i = 0; i < 20; i += 2) {
        pointerA->data = i;
        pointerA->next = (Node*)malloc(sizeof(Node));
        pointerA = (Node*)pointerA->next;
        pointerB->data = i + 1;
        pointerB->next = (Node*)malloc(sizeof(Node));
        pointerB = (Node*)pointerB->next;
    }
    pointerA->data = 20;
    pointerA->next = NULL;
    pointerB->data = 21;
    pointerB->next = NULL;// = concatenate(listA, listB);
    //change(&result);
    Node*printerPointer = listA;
    while (1) {
        if (printerPointer == NULL) break;
        printf(" %d", printerPointer->data);
        printerPointer = (Node*)printerPointer->next;
    }
    getchar();
    //rearrange(result);
    //RemoveRange(result, 3, 12);
    Node* result = merge(listA, listB);
    while (1) {
        if (result == NULL) break;
        printf(" %d", result->data);
        result = (Node*)result->next;
    }
    getchar();
}
