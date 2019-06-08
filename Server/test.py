# Test functions

def encrypt(data: str) -> str:
    result: bytearray = bytearray(data, 'ascii')

    i: int = 0
    while i < len(result):
        temp = result[i] % 3
        if temp == 0:
            result[i] = result[i] + 3
        elif temp == 1:
            result[i] = result[i] - 6
        elif temp == 2:
            result[i] = result[i] - 12
        i += 1

    return result.decode('ascii')


def decrypt(data: str) -> str:
    result: bytearray = bytearray(data, 'ascii')

    i: int = 0
    while i < len(result):
        temp = result[i] % 3
        if temp == 0:
            result[i] = result[i] - 3
        elif temp == 1:
            result[i] = result[i] + 6
        elif temp == 2:
            result[i] = result[i] + 12
        i += 1

    return result.decode('ascii')


if __name__ == "__main__":
    test_str: str = "Test string xyz XYZ"
    print(test_str)
    encrypted: str = encrypt(test_str)
    print(encrypted)
    print(decrypt(encrypted))

    pass
