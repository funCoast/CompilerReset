package llvm;

public enum RetType {
    i32,
    i8,
    i1,
    i32_,
    i8_,
    VOID;

    public RetType toPoint() {
        if (this == i32) {
            return i32_;
        } else if (this == i8) {
            return i8_;
        }
        return i32_;
    }

    public RetType toBasic() {
        if (this == i32_) {
            return i32;
        } else if (this == i8_) {
            return i8;
        }
        return i32;
    }

    @Override
    public String toString() {
        if (this == i32_) {
            return "i32*";
        } else if (this == i8_) {
            return "i8*";
        } else {
            return super.toString();
        }
    }
}

