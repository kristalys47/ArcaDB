string_commands = ""

with open(".env", "r") as f:
    lines = f.readlines()
    for n in lines:
        divided = n.split("=")
        property = divided[0]
        value = divided[1].strip()
        string_commands = string_commands + " -D" + property + "=" + value

print(string_commands)
