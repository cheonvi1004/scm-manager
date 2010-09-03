/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class Group implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = 1752369869345245872L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  public Group(String name)
  {
    this.name = name;
    this.members = new ArrayList<String>();
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param members
   */
  public Group(String name, List<String> members)
  {
    this.name = name;
    this.members = members;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  public boolean add(String member)
  {
    return members.add(member);
  }

  /**
   * Method description
   *
   */
  public void clear()
  {
    members.clear();
  }

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  public boolean remove(String member)
  {
    return members.remove(member);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder msg = new StringBuilder();

    msg.append(name).append(" [");

    if (Util.isNotEmpty(members))
    {
      Iterator<String> it = members.iterator();

      while (it.hasNext())
      {
        msg.append(it.next());

        if (it.hasNext())
        {
          msg.append(",");
        }
      }
    }

    return msg.append("]").toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getMembers()
  {
    return members;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<String> members;

  /** Field description */
  private String name;
}
