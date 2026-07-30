def foo(x):
    return x + 1
def main():
    x = None
    x = 0
    while x < 10:
        x = foo(x)
        print(x)

main()
