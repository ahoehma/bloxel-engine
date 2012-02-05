/*******************************************************************************
 * Copyright (c) 2012 Andreas Höhmann
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *******************************************************************************/
package de.bloxel.engine.util;

import static org.apache.commons.lang3.ClassUtils.getPackageCanonicalName;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.bloxel.engine.resources.Resources;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class JAXBUtils {

  public static <T> T unmarschal(final InputStream inputStream, final Class<T> aClass) {
    try {
      // http://jaxb.java.net/faq/index.html#classloader
      final JAXBContext jc = JAXBContext.newInstance(getPackageCanonicalName(Resources.class));
      final Unmarshaller unmarshaller = jc.createUnmarshaller();
      // http://jaxb.java.net/guide/Unmarshalling_is_not_working__Help_.html
      unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
      final XMLInputFactory staxFactory = XMLInputFactory.newInstance();
      final XMLStreamReader xmlReader = staxFactory.createXMLStreamReader(inputStream);
      return unmarshaller.unmarshal(xmlReader, aClass).getValue();
    } catch (final JAXBException e) {
      throw new RuntimeException(String.format("Can't load unmarschal '%s'", aClass), e);
    } catch (final XMLStreamException e) {
      throw new RuntimeException(String.format("Can't load unmarschal '%s'", aClass), e);
    }
  }
}