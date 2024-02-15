/*
 * The MIT License
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ee.ria.xroad.common.certificateprofile.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Implementation of DnFieldDescription that uses localizable labelKeys
 * and uses {@link DnFieldLabelLocalizationKey} enum to define labelKey.
 * Provides backwards compatibility-implementation of {@link #getLabel()}
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EnumLocalizedFieldDescriptionImpl extends LocalizedFieldDescriptionImpl {

    /**
     * For backwards compatibility while we still support old UI.
     * Remove when old UI support can be removed
     */
    @Override
    @Deprecated
    public String getLabel() {
        return labelLocalizationKey.getLabel();
    }

    private final DnFieldLabelLocalizationKey labelLocalizationKey;

    public EnumLocalizedFieldDescriptionImpl(String id,
                                             DnFieldLabelLocalizationKey labelLocalizationKey,
                                             String defaultValue) {
        super(id, labelLocalizationKey.name(), defaultValue);
        this.labelLocalizationKey = labelLocalizationKey;
    }

    public EnumLocalizedFieldDescriptionImpl(String id,
                                             DnFieldLabelLocalizationKey labelLocalizationKey,
                                             String defaultValue,
                                             boolean readOnly,
                                             boolean required) {
        super(id, labelLocalizationKey.name(), defaultValue, readOnly, required);
        this.labelLocalizationKey = labelLocalizationKey;
    }

}
