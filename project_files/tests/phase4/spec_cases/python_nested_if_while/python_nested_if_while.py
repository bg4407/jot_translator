def nested():
    i = None
    i = 0
    while i < 3:
        if i == 1:
            print("mid")
        else:
            print("edge")
        i = i + 1
def main():
    nested()

main()
