package org.omg.bpmn.miwg.xpath.checks;

import org.omg.bpmn.miwg.xpath.common.AbstractXpathCheck;
import org.omg.bpmn.miwg.xpath.common.Direction;

public class C_2_0_Check extends AbstractXpathCheck {

	@Override
	public String getName() {
		return "C.2.0";
	}

	@Override
	public void doExecute() throws Throwable {
		

		selectCollaboration();
		{

			selectPool("Credit Card Company");
			{

				selectReferencedProcess();
				{
					checkAttributeValue("isExecutable", "false");

					navigateElement("bpmn:startEvent", "Receive Credit Card Information");
					checkMessageEvent();
					checkMessageFlow("Send Credit Card Information", Direction.Input, "bpmn:task",
							"Pay Order");
					
					navigateFollowingElement("bpmn:task", "Take Payment");
					
					navigateFollowingElement("bpmn:endEvent", "Send Result");
					checkMessageEvent();
					checkMessageFlow("", Direction.Output, "bpmn:task",
							"Pay Order");
					
				}
				pop();
				
			}
			pop();

		}
		pop();
	}

}
