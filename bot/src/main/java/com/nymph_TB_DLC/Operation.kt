package com.nymph_TB_DLC

enum class Operation {
    DoNothing,
    Dilution,
    Distillation,
    Heating,
    Freezing,
    ReversedPhase,
    Activation,
    Filter,
    Stir,
    CollectProduct;

    companion object {
        fun Int.toOperation() = when (this) {
            0 -> DoNothing
            1 -> Dilution
            2 -> Distillation
            3 -> Heating
            4 -> Freezing
            5 -> ReversedPhase
            6 -> Activation
            7 -> Filter
            8 -> Stir
            9 -> CollectProduct
            else -> null
        }
    }
}