package llvm;

public class StringConstValue {
    int id;
    String value;
    int size;

    public StringConstValue(int id, String value, int size) {
        this.id = id;
        this.value = value;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        // @.str.1 = private unnamed_addr constant [8 x i8] c"Hello: \00", align 1
        return "@.str." + id + " = private unnamed_addr constant [" + size + " x i8] c\"" + value + "\\00\", align 1";
    }
}
