package com.nielsmasdorp.speculum.views;

import com.nielsmasdorp.speculum.models.Configuration;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public interface SetupView extends BaseView {

    void showLoading();

    void hideLoading();

    void navigateToMainActivity(Configuration configuration);
}