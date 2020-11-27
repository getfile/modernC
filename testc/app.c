
#include <stdio.h>
#include "color.h"

typedef struct Role {
	int age;
	void (*walk)(void* obj);
} A;

// 继承Role
typedef struct Avatar {
	// 父类部分
	int age;
	void (*walk)(void* obj);
	// 本类部分
	int weapon;
} B;

// 继承Avatar
typedef struct Hero {
	// 父类部分
	int age;
	void (*walk)(void* obj);
	int weapon;

	// 本类部分
	int bag;
} C;

void A_walk(void* obj)
{
	A* o = (A*)obj;
	printf("age %d\n", o->age);
}

void A_run(A* obj) { }

void B_walk(void* obj)
{
	B* o = (B*)obj;
	printf("age %d and weapon %d\n", o->age, o->weapon);
}

void B_say(B* obj) { }

void main(int num, char* args)
{
	printf("ok\n");
	colorBlend(1, 2, 0.5);

	int m = 9;
	int n = m - -m;
	printf("n = %d\n", n);

	A a;
	a.walk = A_walk;
	a.age = 71;
	a.walk(&a);

	B b;
	b.walk = B_walk;
	b.age = 99;
	a.walk(&b);
	b.walk(&b);
	B_say(&b);

	printf("over\n");
}