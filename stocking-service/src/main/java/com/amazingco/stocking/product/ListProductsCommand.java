package com.amazingco.stocking.product;

import com.amazingco.core.usecase.Command;

public final class ListProductsCommand extends Command {

    public ListProductsCommand(String traceId) {
        super(traceId);
    }
}
