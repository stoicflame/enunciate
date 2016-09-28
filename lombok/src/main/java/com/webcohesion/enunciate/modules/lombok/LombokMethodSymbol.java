package com.webcohesion.enunciate.modules.lombok;

import com.sun.tools.javac.code.Symbol;

public class LombokMethodSymbol extends Symbol.MethodSymbol {
    public LombokMethodSymbol(VarSymbol varSymbol) {
        super(varSymbol.flags(), varSymbol.name, varSymbol.type, varSymbol.owner);
    }
}
