/**
 *  Copyright 2009 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.opencastproject.workingfilerepository.impl;

import org.opencastproject.util.DocUtil;
import org.opencastproject.util.doc.DocRestData;
import org.opencastproject.util.doc.Format;
import org.opencastproject.util.doc.Param;
import org.opencastproject.util.doc.RestEndpoint;
import org.opencastproject.util.doc.RestTestForm;
import org.opencastproject.util.doc.Status;
import org.opencastproject.workingfilerepository.api.WorkingFileRepository;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class WorkingFileRepositoryRestEndpoint {
  private static final Logger logger = LoggerFactory.getLogger(WorkingFileRepositoryRestEndpoint.class);
  protected WorkingFileRepository repo;

  public void setRepository(WorkingFileRepository repo) {
    this.repo = repo;
  }

  public WorkingFileRepositoryRestEndpoint() {
    docs = generateDocs();
  }

  protected final String docs;
  private String[] notes = {
          "All paths above are relative to the REST endpoint base (something like http://your.server/files)",
          "If the service is down or not working it will return a status 503, this means the the underlying service is not working and is either restarting or has failed",
          "A status code 500 means a general failure has occurred which is not recoverable and was not anticipated. In other words, there is a bug! You should file an error report with your server logs from the time when the error occurred: <a href=\"https://issues.opencastproject.org\">Opencast Issue Tracker</a>", };

  private String generateDocs() {
    DocRestData data = new DocRestData("workingfilerepository", "Working file repository", "/files", notes);

    // put
    RestEndpoint endpoint = new RestEndpoint("put", RestEndpoint.Method.POST,
            "/{mediaPackageID}/{mediaPackageElementID}",
            "Store a file in working repository under ./mediaPackageID/mediaPackageElementID");
    endpoint.addPathParam(new Param("mediaPackageID", Param.Type.STRING, null,
            "ID of the media package under which file will be stored"));
    endpoint.addPathParam(new Param("mediaPackageElementID", Param.Type.STRING, null,
            "ID of the element under which file will be stored"));
    endpoint.addBodyParam(true, null, "File that we want to store");
    endpoint.addFormat(new Format("HTML", null, null));
    endpoint.addStatus(Status.OK("Message of successful storage with url to the stored file"));
    endpoint.addStatus(new Status(400, "No file to store, invalid file location"));
    endpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.WRITE, endpoint);

    // delete
    endpoint = new RestEndpoint("deleteViaHttp", RestEndpoint.Method.DELETE,
            "/{mediaPackageID}/{mediaPackageElementID}",
            "Delete media package element identified by mediaPackageID and MediaPackageElementID");
    endpoint.addPathParam(new Param("mediaPackageID", Param.Type.STRING, null,
            "ID of the media package where element is"));
    endpoint.addPathParam(new Param("mediaPackageElementID", Param.Type.STRING, null,
            "ID of the element that will be deleted"));
    endpoint.addStatus(Status.OK("If given file exists it is deleted"));
    endpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.WRITE, endpoint);

    // get
    endpoint = new RestEndpoint("get", RestEndpoint.Method.GET, "/{mediaPackageID}/{mediaPackageElementID}",
            "Retrieve the file stored in working repository under ./mediaPackageID/MediaPackageElementID");
    endpoint.addPathParam(new Param("mediaPackageID", Param.Type.STRING, null,
            "ID of the media package with desired element"));
    endpoint.addPathParam(new Param("mediaPackageElementID", Param.Type.STRING, null, "ID of desired element"));
    // endpoint.addFormat(new Format(".*", "Data that is stored in this location", null));
    endpoint.addStatus(Status.OK("Results in a header with retrieved file"));
    endpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.READ, endpoint);

    // get with filename
    endpoint = new RestEndpoint("get_with_filename", RestEndpoint.Method.GET,
            "/{mediaPackageID}/{mediaPackageElementID}/{fileName}",
            "Retrieve the file stored in working repository under ./mediaPackageID/MediaPackageElementID");
    endpoint.addPathParam(new Param("mediaPackageID", Param.Type.STRING, null,
            "ID of the media package with desired element"));
    endpoint.addPathParam(new Param("mediaPackageElementID", Param.Type.STRING, null, "ID of desired element"));
    endpoint.addPathParam(new Param("fileName", Param.Type.STRING, null, "Name under which the file will be retrieved"));
    // endpoint.addFormat(new Format(".*", "Data that is stored in this location", null));
    endpoint.addStatus(Status.OK("Results in a header with retrieved file"));
    endpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.READ, endpoint);

    return DocUtil.generate(data);
  }

  @POST
  @Produces(MediaType.TEXT_HTML)
  @Path("{mediaPackageID}/{mediaPackageElementID}")
  public Response put(@PathParam("mediaPackageID") String mediaPackageID,
          @PathParam("mediaPackageElementID") String mediaPackageElementID, @Context HttpServletRequest request)
          throws Exception {
    checkService();
    if (ServletFileUpload.isMultipartContent(request)) {
      for (FileItemIterator iter = new ServletFileUpload().getItemIterator(request); iter.hasNext();) {
        FileItemStream item = iter.next();
        if (item.isFormField())
          continue;
        URI url = repo.put(mediaPackageID, mediaPackageElementID, item.getName(), item.openStream());
        return Response.ok("File stored at " + url.toString()).build();
      }
    }
    return Response.serverError().status(400).build();
  }

  @DELETE
  @Produces(MediaType.TEXT_HTML)
  @Path("{mediaPackageID}/{mediaPackageElementID}")
  public Response deleteViaHttp(@PathParam("mediaPackageID") String mediaPackageID,
          @PathParam("mediaPackageElementID") String mediaPackageElementID) {
    checkService();
    repo.delete(mediaPackageID, mediaPackageElementID);
    return Response.ok().build();
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("{mediaPackageID}/{mediaPackageElementID}")
  public Response get(@PathParam("mediaPackageID") String mediaPackageID,
          @PathParam("mediaPackageElementID") String mediaPackageElementID) {
    checkService();
    URI url = repo.getURI(mediaPackageID, mediaPackageElementID);
    String fileName = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
    return Response.ok().header("Content-disposition", "attachment; filename=" + fileName).entity(
            repo.get(mediaPackageID, mediaPackageElementID)).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("{mediaPackageID}/{mediaPackageElementID}/{fileName}")
  public Response get(@PathParam("mediaPackageID") String mediaPackageID,
          @PathParam("mediaPackageElementID") String mediaPackageElementID, @PathParam("fileName") String fileName) {
    checkService();
    return Response.ok().header("Content-disposition", "attachment; filename=" + fileName).entity(
            repo.get(mediaPackageID, mediaPackageElementID)).build();
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("docs")
  public String getDocumentation() {
    return docs;
  }

  protected void checkService() {
    if (repo == null) {
      // TODO What should we do in this case?
      throw new RuntimeException("Working File Repository is currently unavailable");
    }
  }
}
