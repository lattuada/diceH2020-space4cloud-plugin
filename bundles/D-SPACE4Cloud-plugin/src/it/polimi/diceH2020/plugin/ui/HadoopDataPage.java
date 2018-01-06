/*
Copyright 2017 Arlind Rufi
Copyright 2017 Gianmario Pozzi
Copyright 2017 Giorgio Pea

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package it.polimi.diceH2020.plugin.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import it.polimi.diceH2020.plugin.control.Configuration;

public class HadoopDataPage extends WizardPage {
	
	private Composite container;
	private GridLayout layout;
	private Text thinkText, hlowText, hupText, deadlineText, jobPenaltyText;
	private Label jobPenaltyLabel, thinkLabel, deadlineLabel, hupLabel, hlowLabel;
	
	private Map<String, String> parameters;
	private int thinkTime, hlow, hup, deadline;
	private float jobPenalty;
	private Configuration currentConf;

	protected HadoopDataPage(String pageName) {
		super("Select data for hadoop Technology");
		currentConf = Configuration.getCurrent();
		parameters = new HashMap<String, String>();
		setTitle(pageName);
		resetParameters();
	}

	@Override
	public void createControl(Composite arg0) {

		container = new Composite(arg0, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		thinkLabel = new Label(container, SWT.None);
		thinkLabel.setText("Set Think Time [ms]");
		thinkText = new Text(container, SWT.BORDER);
		thinkText.setEnabled(false);

		deadlineLabel = new Label(container, SWT.None);
		deadlineLabel.setText("Set deadline [ms]");
		deadlineText = new Text(container, SWT.BORDER);
		deadlineText.setEditable(true);

		hlowLabel = new Label(container, SWT.None);
		hlowLabel.setText("Set minimum level of concurrency");
		hlowText = new Text(container, SWT.BORDER);
		hlowText.setEditable(true);

		hupLabel = new Label(container, SWT.None);
		hupLabel.setText("Set maximum level of concurrency");
		hupText = new Text(container, SWT.BORDER);
		hupText.setEditable(true);

		jobPenaltyLabel = new Label(container, SWT.None);
		jobPenaltyLabel.setText("Set job penalty cost [$/job]");
		jobPenaltyText = new Text(container, SWT.BORDER);
		jobPenaltyText.setEditable(true);

		
		/*
		 * Listeners
		 */
		
        deadlineText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
            	if (!deadlineText.getText().isEmpty())
            		deadline = Integer.parseInt(deadlineText.getText()); 
            	getWizard().getContainer().updateButtons();
            }
        });
        
        hlowText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
            	if (!hlowText.getText().isEmpty())
            		hlow = Integer.parseInt(hlowText.getText());
            	getWizard().getContainer().updateButtons();
            }
        });
        
        hupText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
            	if (!hupText.getText().isEmpty())
            		hup = Integer.parseInt(hupText.getText());
            	getWizard().getContainer().updateButtons();
            }
        });
		
		jobPenaltyText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				try {
        			if (!jobPenaltyText.getText().isEmpty()){
        				jobPenalty = Float.parseFloat(jobPenaltyText.getText());
        				setErrorMessage(null);
        			}
					
				} catch (NumberFormatException e) {
					setErrorMessage("Incorrect value for Job Penalty");
					jobPenaltyText.setFocus();
				}
				getWizard().getContainer().updateButtons();
			}
		});
		
        deadlineText.addListener (SWT.Verify, e -> {
			String string = e.text;
			char [] chars = new char [string.length ()];
			string.getChars (0, chars.length, chars, 0);
			for (int i=0; i<chars.length; i++) {
				if (!(('0' <= chars [i] && chars [i] <= '9'))) {
					e.doit = false;
					return;
				}
			}
		});
        
        hupText.addListener (SWT.Verify, e -> {
			String string = e.text;
			char [] chars = new char [string.length ()];
			string.getChars (0, chars.length, chars, 0);
			for (int i=0; i<chars.length; i++) {
				if (!(('0' <= chars [i] && chars [i] <= '9'))) {
					e.doit = false;
					return;
				}
			}
		});
        
        hlowText.addListener (SWT.Verify, e -> {
			String string = e.text;
			char [] chars = new char [string.length ()];
			string.getChars (0, chars.length, chars, 0);
			for (int i=0; i<chars.length; i++) {
				if (!(('0' <= chars [i] && chars [i] <= '9'))) {
					e.doit = false;
					return;
				}
			}
		});
        
        jobPenaltyText.addListener (SWT.Verify, e -> {
			String string = e.text;
			char [] chars = new char [string.length ()];
			string.getChars (0, chars.length, chars, 0);
			for (int i=0; i<chars.length; i++) {
				if (!(('0' <= chars [i] && chars [i] <= '9') || chars[i] == '.')) {
					e.doit = false;
					return;
				}
			}
		});
		
		setControl(container);
		setPageComplete(false);
	
	}

   @Override
   public boolean canFlipToNextPage() {

      if (deadline > 0 && thinkTime > 0 && hup > 0 && hlow > 0 && hlow <= hup){

         if (Configuration.getCurrent().isPrivate()) {

            if (Configuration.getCurrent().hasAdmissionControl() && jobPenalty == -1)
               return false;

            if (!Configuration.getCurrent().hasAdmissionControl() && hlow != hup) {
               setErrorMessage("Minimum and maximum level of concurrency must coincide");
               return false;
            }

            setErrorMessage("");
            setParameters();
            return true;
         }
         else {
            // Public
            if (hlow != hup) {
               setErrorMessage("Minimum and maximum level of concurrency must coincide");
               return false;
            }
            setParameters();
            return true;
         }
      }

      return false;
   }

	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(){
		
		parameters.put("d", Integer.toString(deadline));
		parameters.put("hlow", Integer.toString(hlow));
		parameters.put("hup", Integer.toString(hup));
		parameters.put("think", Integer.toString(thinkTime));
		
		if (currentConf.isPrivate())
			parameters.put("penalty", Float.toString(jobPenalty));
	}
	
	public void reset() {
		
		hlowText.setText("");
		hupText.setText("");
		jobPenaltyText.setText("");
		deadlineText.setText("");
		
		parameters.clear();
		resetParameters();
		
		if (Configuration.getCurrent().isPrivate()) {
			privateCase();
		} else {
			publicCase();
		}
	}
	
	private void resetParameters() {
		hlow = -1;
		hup = -1;
		jobPenalty = -1;
		deadline = -1;
	}

    public void setThinkTime(String thinkTimeInput){
    	
    	thinkText.setText(thinkTimeInput);
		thinkText.setEnabled(false);
		thinkText.setEditable(false);
		
		if (thinkTimeInput.equals("0")){
			thinkTime = 1;		 
		}
		else {
			thinkTime = Integer.parseInt(thinkTimeInput);
		}	
		return;
	}
    
	public void privateCase() {
		jobPenaltyLabel.setVisible(true);
		jobPenaltyText.setVisible(true);
		if (Configuration.getCurrent().hasAdmissionControl() == false) {
			jobPenaltyText.setEditable(false);
			jobPenaltyText.setEnabled(false);
		}
	}

	public void publicCase() {
		jobPenaltyLabel.setVisible(false);
		jobPenaltyText.setVisible(false);
	}
}
