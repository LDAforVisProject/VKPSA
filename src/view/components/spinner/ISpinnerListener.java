package view.components.spinner;

import java.math.BigDecimal;

public interface ISpinnerListener
{
	/**
	 * Processes new value from spinner.s
	 * @param value
	 */
	public void processSpinnerValue(BigDecimal value, String id);
}
