/**
 * The MIT License
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.cs.admin.rest.api.converter;

import lombok.RequiredArgsConstructor;
import org.niis.xroad.cs.admin.api.domain.GlobalGroupMember;
import org.niis.xroad.cs.openapi.model.GroupMembersFilterModelDto;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class GroupMemberFilterModelConverter {

    public GroupMembersFilterModelDto convert(List<GlobalGroupMember> members) {

        var result = new GroupMembersFilterModelDto();
        result.setInstances(members.stream()
                .map(m -> m.getIdentifier().getXRoadInstance())
                .distinct()
                .collect(toList()));
        result.setMemberClasses(members.stream()
                .map(m -> m.getIdentifier().getMemberClass())
                .distinct()
                .collect(toList()));
        result.setCodes(members.stream()
                .map(m -> m.getIdentifier().getMemberCode())
                .distinct()
                .collect(toList()));
        result.setSubsystems(members.stream()
                .map(m -> m.getIdentifier().getSubsystemCode())
                .distinct()
                .collect(toList()));
        return result;
    }
}
