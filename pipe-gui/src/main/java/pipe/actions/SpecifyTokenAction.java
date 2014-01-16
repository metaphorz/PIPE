package pipe.actions;

import pipe.controllers.PipeApplicationController;
import pipe.gui.ApplicationSettings;
import pipe.gui.TokenDialog;
import pipe.gui.TokenPanel;
import pipe.views.PipeApplicationView;
import pipe.views.TokenView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
//
// Steve Doubleday: refactored to simplify testing
//
public class SpecifyTokenAction extends GuiAction
{
	private static final long serialVersionUID = 1L;
	protected static final String PROBLEM_ENCOUNTERED_SAVING_UPDATES = "Problem encountered saving updates to tokens.  Changes will be discarded; please re-enter.\n";
	private String errorMessage;
	private TokenPanel dialogContent;
	private final PipeApplicationView pipeApplicationView;
    private final PipeApplicationController pipeApplicationController;
	private JDialog guiDialog;
	private ActionEvent forcedAction;

	public SpecifyTokenAction(PipeApplicationView applicationView, PipeApplicationController pipeApplicationController)
    {
        super("SpecifyTokenClasses", "Specify tokens", "shift ctrl T");
        this.pipeApplicationView = applicationView;
        this.pipeApplicationController = pipeApplicationController;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        buildTokenGuiClasses();
        finishBuildingGui();
    }


	protected void filterValidTokenViews(LinkedList<TokenView> tokenViews,
			TokenView tc)
	{
		// Only add TokenViews that are enabled with a non-blank ID
		if (tc.isValid()) tokenViews.add(tc);
	}
	protected void updateTokenViews(LinkedList<TokenView> tokenViews)
	{
		try
		{
			pipeApplicationView.getCurrentPetriNetView().updateOrReplaceTokenViews(tokenViews);
		}
		catch (Exception e)
		{
			setErrorMessage(PROBLEM_ENCOUNTERED_SAVING_UPDATES +
					"Details: "+e.getMessage());
			showWarningAndReEnterTokenDialog();
		}
	}
	public void buildTokenGuiClasses()
	{
		dialogContent = new TokenPanel(pipeApplicationController.getActivePetriNetController());
		guiDialog = new TokenDialog(pipeApplicationView, "Tokens", true, dialogContent);
	}

	public void finishBuildingGui()
	{
		guiDialog.setSize(600, 200);
		guiDialog.setLocationRelativeTo(null);
		dialogContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		dialogContent.setOpaque(true);

		JPanel buttonPane = new JPanel();
		buttonPane
		.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane
		.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		JButton ok = new JButton("OK");
		ok.addActionListener((ActionListener) guiDialog);
		buttonPane.add(ok);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener((ActionListener) guiDialog);
		buttonPane.add(cancel);

		guiDialog.add(dialogContent, BorderLayout.CENTER);
		guiDialog.add(buttonPane, BorderLayout.PAGE_END);
		dialogContent.setVisible(true);

		if (forcedAction != null) forceContinue();
		else guiDialog.setVisible(true);
	}
	private void forceContinue()
	{
		((TokenDialog) guiDialog).actionPerformed(forcedAction);
		forcedAction = null;
	}

	protected void showWarningAndReEnterTokenDialog()
	{
		JOptionPane
		.showMessageDialog(
				new JPanel(),
				getErrorMessage(),
				"Warning",
				JOptionPane.WARNING_MESSAGE);
		setErrorMessage("");
		actionPerformed(null);
	}

	protected void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	protected String getErrorMessage()
	{
		return this.errorMessage;
	}
}
