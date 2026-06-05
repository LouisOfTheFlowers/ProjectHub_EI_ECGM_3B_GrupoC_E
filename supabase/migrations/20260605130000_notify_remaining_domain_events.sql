create or replace function public.current_public_user_id()
returns integer
language sql
stable
security definer
set search_path = public
as $$
  select u.id
  from public.users u
  where u.email = auth.jwt() ->> 'email'
  limit 1
$$;

create or replace function public.notify_admins_of_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  select
    admin_user.id,
    'Novo utilizador registado',
    'A conta de ' || coalesce(nullif(new.nome, ''), new.username, new.email) || ' foi criada.',
    'ACCOUNT',
    'admin/teams',
    new.id
  from public.users admin_user
  where upper(admin_user.role) = 'ADMIN'
    and admin_user.id <> new.id;

  return new;
end;
$$;

drop trigger if exists notify_admins_of_new_user_trigger on public.users;

create trigger notify_admins_of_new_user_trigger
after insert on public.users
for each row
execute function public.notify_admins_of_new_user();

create or replace function public.notify_user_role_changed()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if old.role is not distinct from new.role then
    return new;
  end if;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    new.id,
    'Perfil atualizado',
    'A tua role foi alterada para ' || new.role || '.',
    'ACCOUNT',
    case
      when upper(new.role) = 'GESTOR' then 'gestor/profile'
      when upper(new.role) = 'ADMIN' then 'admin/profile'
      else 'user/profile'
    end,
    new.id
  );

  return new;
end;
$$;

drop trigger if exists notify_user_role_changed_trigger on public.users;

create trigger notify_user_role_changed_trigger
after update of role on public.users
for each row
execute function public.notify_user_role_changed();

create or replace function public.notify_project_completed()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if upper(coalesce(new.status, '')) not in (
    'CONCLUIDO',
    'CONCLUIDA',
    'COMPLETO',
    'COMPLETA',
    'FINALIZADO',
    'FINALIZADA'
  ) then
    return new;
  end if;

  if upper(coalesce(old.status, '')) in (
    'CONCLUIDO',
    'CONCLUIDA',
    'COMPLETO',
    'COMPLETA',
    'FINALIZADO',
    'FINALIZADA'
  ) then
    return new;
  end if;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  select distinct
    member_ids.user_id,
    'Projeto concluído',
    'O projeto "' || new.nome || '" foi concluído.',
    'PROJECT',
    'user/projects',
    new.id
  from (
    select pu.user_id
    from public.projeto_users pu
    where pu.projeto_id = new.id
    union
    select tu.user_id
    from public.tarefa_users tu
    inner join public.tarefas t on t.id = tu.tarefa_id
    where t.projeto_id = new.id
  ) member_ids;

  return new;
end;
$$;

drop trigger if exists notify_project_completed_trigger on public.projetos;

create trigger notify_project_completed_trigger
after update of status on public.projetos
for each row
execute function public.notify_project_completed();

create or replace function public.notify_task_created_manager()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  manager_id integer;
  project_name text;
  actor_id integer;
begin
  select p.gestor_id, p.nome
  into manager_id, project_name
  from public.projetos p
  where p.id = new.projeto_id;

  actor_id := public.current_public_user_id();

  if manager_id is null or manager_id is not distinct from actor_id then
    return new;
  end if;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    manager_id,
    'Nova tarefa no projeto',
    'Foi criada a tarefa "' || new.titulo || '" no projeto "' || coalesce(project_name, 'sem nome') || '".',
    'TASK',
    'gestor/tasks',
    new.id
  );

  return new;
end;
$$;

drop trigger if exists notify_task_created_manager_trigger on public.tarefas;

create trigger notify_task_created_manager_trigger
after insert on public.tarefas
for each row
execute function public.notify_task_created_manager();

create or replace function public.notify_task_updated_assignees()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if old.titulo is not distinct from new.titulo
    and old.descricao is not distinct from new.descricao
    and old.data_inicio is not distinct from new.data_inicio
    and old.data_fim is not distinct from new.data_fim
    and old.projeto_id is not distinct from new.projeto_id then
    return new;
  end if;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  select distinct
    tu.user_id,
    'Tarefa atualizada',
    'A tarefa "' || new.titulo || '" foi atualizada.',
    'TASK',
    'user/tasks',
    new.id
  from public.tarefa_users tu
  where tu.tarefa_id = new.id;

  return new;
end;
$$;

drop trigger if exists notify_task_updated_assignees_trigger on public.tarefas;

create trigger notify_task_updated_assignees_trigger
after update on public.tarefas
for each row
execute function public.notify_task_updated_assignees();

create or replace function public.notify_task_assignee_added()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  task_title text;
  project_name text;
begin
  select t.titulo, p.nome
  into task_title, project_name
  from public.tarefas t
  left join public.projetos p on p.id = t.projeto_id
  where t.id = new.tarefa_id;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    new.user_id,
    'Nova tarefa atribuída',
    'Foi-te atribuída a tarefa "' || coalesce(task_title, 'sem título') || '" em "' || coalesce(project_name, 'sem projeto') || '".',
    'TASK',
    'user/tasks',
    new.tarefa_id
  );

  return new;
end;
$$;

drop trigger if exists notify_task_assignee_added_trigger on public.tarefa_users;

create trigger notify_task_assignee_added_trigger
after insert on public.tarefa_users
for each row
execute function public.notify_task_assignee_added();

create or replace function public.notify_task_assignee_removed()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  task_title text;
begin
  select t.titulo
  into task_title
  from public.tarefas t
  where t.id = old.tarefa_id;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    old.user_id,
    'Tarefa removida',
    'Deixaste de estar associado à tarefa "' || coalesce(task_title, 'sem título') || '".',
    'TASK',
    'user/tasks',
    old.tarefa_id
  );

  return old;
end;
$$;

drop trigger if exists notify_task_assignee_removed_trigger on public.tarefa_users;

create trigger notify_task_assignee_removed_trigger
before delete on public.tarefa_users
for each row
execute function public.notify_task_assignee_removed();

create or replace function public.notify_manager_of_task_progress_record()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  manager_id integer;
  task_title text;
begin
  if new.taxa_conclusao < 100 then
    return new;
  end if;

  select p.gestor_id, t.titulo
  into manager_id, task_title
  from public.tarefas t
  inner join public.projetos p on p.id = t.projeto_id
  where t.id = new.tarefa_id;

  if manager_id is null then
    return new;
  end if;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    manager_id,
    'Tarefa concluída',
    'A tarefa "' || coalesce(task_title, 'sem título') || '" foi marcada como concluída.',
    'TASK',
    'gestor/tasks',
    new.tarefa_id
  );

  return new;
end;
$$;

drop trigger if exists notify_manager_of_task_progress_record_trigger on public.registos_tarefa;

create trigger notify_manager_of_task_progress_record_trigger
after insert on public.registos_tarefa
for each row
execute function public.notify_manager_of_task_progress_record();

create or replace function public.notify_manager_of_observation()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  manager_id integer;
  task_id integer;
  task_title text;
begin
  select p.gestor_id, t.id, t.titulo
  into manager_id, task_id, task_title
  from public.registos_tarefa rt
  inner join public.tarefas t on t.id = rt.tarefa_id
  inner join public.projetos p on p.id = t.projeto_id
  where rt.id = new.registo_id;

  if manager_id is null then
    return new;
  end if;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    manager_id,
    'Nova observação',
    'Foi adicionada uma observação à tarefa "' || coalesce(task_title, 'sem título') || '".',
    'OBSERVATION',
    'gestor/tasks',
    task_id
  );

  return new;
end;
$$;

drop trigger if exists notify_manager_of_observation_trigger on public.observacoes;

create trigger notify_manager_of_observation_trigger
after insert on public.observacoes
for each row
execute function public.notify_manager_of_observation();
