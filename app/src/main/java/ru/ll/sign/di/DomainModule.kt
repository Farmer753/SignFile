package ru.ll.sign.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.ll.sign.ui.resultDialog.ResultDialogViewModel
import ru.ll.sign.ui.sign.SignViewModel

fun domainModule() = module {
    viewModel { SignViewModel() }
    viewModel { ResultDialogViewModel() }
}