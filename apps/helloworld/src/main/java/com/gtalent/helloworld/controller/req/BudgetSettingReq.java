package com.gtalent.helloworld.controller.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BudgetSettingReq {

    @NotNull
    @Min(0)
    private Integer threshold;

    @NotNull
    @Min(0) @Max(23)
    private Integer alertHour;

    @NotNull
    @Min(0) @Max(59)
    private Integer alertMinute;

    private boolean enabled = true;

    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }

    public Integer getAlertHour() { return alertHour; }
    public void setAlertHour(Integer alertHour) { this.alertHour = alertHour; }

    public Integer getAlertMinute() { return alertMinute; }
    public void setAlertMinute(Integer alertMinute) { this.alertMinute = alertMinute; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
