/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package ch.elexis.core.osgi;

import java.util.Collection;

import org.osgi.framework.Bundle;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;

/**
 * @author Tina Tian
 * @see <a href=
 *      "https://help.liferay.com/hc/en-us/articles/360018162771-Detecting-Unresolved-OSGi-Components#declarative-services-unsatisfied-component-scanner">
 *      Detecting Unresolved OSGi Components </a>
 */
public class UnsatisfiedComponentUtil {

	public static String listUnsatisfiedComponents(ServiceComponentRuntime serviceComponentRuntime, Bundle... bundles) {
		StringBuilder sb = new StringBuilder();
		for (Bundle bundle : bundles) {
			_listUnsatisfiedComponents(serviceComponentRuntime, bundle, sb);
		}
		return sb.toString();
	}

	private static String _collectUnsatisfiedInformation(ServiceComponentRuntime serviceComponentRuntime,
			Bundle bundle) {

		StringBuilder sb = new StringBuilder();

		Collection<ComponentDescriptionDTO> componentDescriptionDTOs = serviceComponentRuntime
				.getComponentDescriptionDTOs(bundle);
		for (ComponentDescriptionDTO componentDescriptionDTO : componentDescriptionDTOs) {
			Collection<ComponentConfigurationDTO> componentConfigurationDTOs = serviceComponentRuntime
					.getComponentConfigurationDTOs(componentDescriptionDTO);
			for (ComponentConfigurationDTO componentConfigurationDTO : componentConfigurationDTOs) {
				if (componentConfigurationDTO.state == ComponentConfigurationDTO.UNSATISFIED_REFERENCE) {
					_collectUnsatisfiedReferences(componentDescriptionDTO, componentConfigurationDTO, sb);
				}
			}
		}

		return sb.toString();
	}

	private static void _collectUnsatisfiedReferences(ComponentDescriptionDTO componentDescriptionDTO,
			ComponentConfigurationDTO componentConfigurationDTO, StringBuilder sb) {

		sb.append("\n\tDeclarative Service {id: ");
		sb.append(componentConfigurationDTO.id);
		sb.append(", name: ");
		sb.append(componentDescriptionDTO.name);
		sb.append(", unsatisfied references: ");

		for (UnsatisfiedReferenceDTO unsatisfiedReferenceDTO : componentConfigurationDTO.unsatisfiedReferences) {
			sb.append("\n\t\t{name: ");
			sb.append(unsatisfiedReferenceDTO.name);
			sb.append(", target: ");
			sb.append(unsatisfiedReferenceDTO.target);
			sb.append("}");
		}

		sb.append("\n\t}");
	}

	private static void _listUnsatisfiedComponents(ServiceComponentRuntime serviceComponentRuntime, Bundle bundle,
			StringBuilder sb) {

		if (bundle.getState() != Bundle.ACTIVE) {
			return;
		}

		String unsatisfiedInformation = _collectUnsatisfiedInformation(serviceComponentRuntime, bundle);
		if (unsatisfiedInformation.isEmpty()) {
			return;
		}

		sb.append("\nBundle {id: ");
		sb.append(bundle.getBundleId());
		sb.append(", name: ");
		sb.append(bundle.getSymbolicName());
		sb.append(", version: ");
		sb.append(bundle.getVersion());
		sb.append("}");
		sb.append(unsatisfiedInformation);
	}

}