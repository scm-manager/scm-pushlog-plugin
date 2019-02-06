package sonia.scm.pushlog;

import de.otto.edison.hal.HalRepresentation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@SuppressWarnings("squid:S2160") // We do not need an equals method
public class CommitterHalRepresentation extends HalRepresentation {
    private String name;
}
